package com.farmtofolk.farmtofolk_ledger.farmer;

import com.farmtofolk.farmtofolk_ledger.auth.User;
import com.farmtofolk.farmtofolk_ledger.auth.UserRepository;
import com.farmtofolk.farmtofolk_ledger.auth.UserRole;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import com.farmtofolk.farmtofolk_ledger.events.DomainEventPublisher;
import com.farmtofolk.farmtofolk_ledger.events.ImageUploadedEvent;
import com.farmtofolk.farmtofolk_ledger.events.PublicTraceContentChangedEvent;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import com.farmtofolk.farmtofolk_ledger.storage.StoredFileResponse;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FarmerService {

  private static final Set<String> PROFILE_PHOTO_CONTENT_TYPES =
      Set.of("image/jpeg", "image/png", "image/webp");
  private static final Set<String> INTRO_VIDEO_CONTENT_TYPES =
      Set.of("video/mp4", "video/quicktime");

  private final FarmerRepository farmerRepository;
  private final StorageService storageService;
  private final DomainEventPublisher domainEventPublisher;
  private final TransactionTemplate transactionTemplate;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final String defaultUserPassword;

  public FarmerService(
      FarmerRepository farmerRepository,
      StorageService storageService,
      DomainEventPublisher domainEventPublisher,
      PlatformTransactionManager transactionManager,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      @Value("${app.security.default-user-password:ChangeMe@123}")
          String defaultUserPassword) {
    this.farmerRepository = farmerRepository;
    this.storageService = storageService;
    this.domainEventPublisher = domainEventPublisher;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.defaultUserPassword = defaultUserPassword;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.transactionTemplate.setPropagationBehavior(
        TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  @Transactional
  public FarmerResponse createFarmer(CreateFarmerRequest request) {
    // Copy request data into a new Farmer entity.
    Farmer farmer = new Farmer();
    applyRequest(farmer, request);
    if (farmer.getFarmerCode() == null || farmer.getFarmerCode().isBlank()) {
      farmer.setFarmerCode(generateFarmerCode());
    }
    validateUniqueFields(farmer.getFarmerCode(), farmer.getPhone(), null);
    if (userRepository.existsByPhone(farmer.getPhone())) {
      throw new ConflictException("Farmer phone already has a user account");
    }

    User user = new User();
    user.setName(farmer.getName());
    user.setPhone(farmer.getPhone());
    user.setRole(UserRole.FARMER);
    user.setActive(true);
    user.setPasswordHash(passwordEncoder.encode(defaultUserPassword));
    User savedUser = userRepository.save(user);
    farmer.setUserId(savedUser.getId());

    // Farmer and login account are committed atomically.
    Farmer savedFarmer = farmerRepository.save(farmer);
    return FarmerResponse.from(savedFarmer, storageService);
  }

  public FarmerResponse uploadProfilePhoto(UUID farmerId, MultipartFile file) {
    findFarmer(farmerId);
    StoredFileResponse storedFile =
        storageService.upload(
            file, "farmers/" + farmerId + "/profile-photo", PROFILE_PHOTO_CONTENT_TYPES);
    FarmerResponse response =
        saveUploadedFarmerFile(
            farmerId, storedFile, farmer -> {
              farmer.setProfilePhotoKey(storedFile.objectKey());
              farmer.setProfilePhotoUrl(null);
            });
    domainEventPublisher.publishAfterCommit(
        new ImageUploadedEvent("FARMER_PROFILE", farmerId, storedFile.objectKey()));
    publishFarmerChanged(farmerId);
    return response;
  }

  public FarmerResponse uploadIntroVideo(UUID farmerId, MultipartFile file) {
    findFarmer(farmerId);
    StoredFileResponse storedFile =
        storageService.upload(
            file, "farmers/" + farmerId + "/intro-video", INTRO_VIDEO_CONTENT_TYPES);
    FarmerResponse response =
        saveUploadedFarmerFile(
            farmerId, storedFile, farmer -> {
              farmer.setIntroVideoKey(storedFile.objectKey());
              farmer.setIntroVideoUrl(null);
            });
    publishFarmerChanged(farmerId);
    return response;
  }

  @Transactional(readOnly = true)
  public FarmerResponse getFarmer(UUID farmerId) {
    // Load one farmer by ID and convert it to a response.
    Farmer farmer = findFarmer(farmerId);
    return FarmerResponse.from(farmer, storageService);
  }

  @Transactional(readOnly = true)
  public List<FarmerResponse> getAllFarmers() {
    // Fetch all farmers and convert each one to a response.
    return farmerRepository.findAll().stream()
        .map(farmer -> FarmerResponse.from(farmer, storageService)).toList();
  }

  @Transactional
  public FarmerResponse updateFarmer(UUID farmerId, CreateFarmerRequest request) {
    // Load the existing farmer, update its fields, then save it.
    Farmer farmer = findFarmer(farmerId);
    applyRequest(farmer, request);
    validateUniqueFields(farmer.getFarmerCode(), farmer.getPhone(), farmerId);
    syncLinkedUser(farmer);

    Farmer savedFarmer = saveAndFlushFarmer(farmer);
    publishFarmerChanged(farmerId);
    return FarmerResponse.from(savedFarmer, storageService);
  }

  @Transactional
  public FarmerResponse updateFarmerStatus(UUID farmerId, UpdateFarmerStatusRequest request) {
    // Load the farmer and update only the active status.
    Farmer farmer = findFarmer(farmerId);
    farmer.setActive(request.active());
    if (farmer.getUserId() != null) {
      userRepository.findById(farmer.getUserId()).ifPresent(user -> user.setActive(request.active()));
    }

    Farmer savedFarmer = saveAndFlushFarmer(farmer);
    publishFarmerChanged(farmerId);
    return FarmerResponse.from(savedFarmer, storageService);
  }

  private Farmer findFarmer(UUID farmerId) {
    // Reuse one not-found lookup rule for all farmer reads and updates.
    return farmerRepository
        .findById(farmerId)
        .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));
  }

  private void applyRequest(Farmer farmer, CreateFarmerRequest request) {
    // Keep request-to-entity field mapping in one place.
    if (request.farmerCode() != null && !request.farmerCode().isBlank()) {
      farmer.setFarmerCode(request.farmerCode().trim());
    }
    farmer.setName(request.name());
    String normalizedPhone = request.phone() == null ? null : request.phone().trim();
    farmer.setPhone(normalizedPhone == null || normalizedPhone.isBlank() ? null : normalizedPhone);
    farmer.setVillage(request.village());
    farmer.setDistrict(request.district());
    farmer.setState(request.state());
    farmer.setBio(request.bio());
    farmer.setProfilePhotoUrl(request.profilePhotoUrl());
    farmer.setIntroVideoUrl(request.introVideoUrl());
    farmer.setJoinedDate(request.joinedDate());
  }

  private String generateFarmerCode() {
    int year = java.time.LocalDate.now().getYear();
    long sequence = farmerRepository.count() + 1;
    String farmerCode = formatFarmerCode(year, sequence);

    while (farmerRepository.existsByFarmerCode(farmerCode)) {
      sequence++;
      farmerCode = formatFarmerCode(year, sequence);
    }

    return farmerCode;
  }

  private String formatFarmerCode(int year, long sequence) {
    return "FTF-FR-" + year + "-" + String.format("%06d", sequence);
  }

  private void validateUniqueFields(String farmerCode, String phone, UUID farmerId) {
    boolean duplicateCode =
        farmerId == null
            ? farmerRepository.existsByFarmerCode(farmerCode)
            : farmerRepository.existsByFarmerCodeAndIdNot(farmerCode, farmerId);
    if (duplicateCode) throw new ConflictException("Farmer code already exists");

    if (phone == null || phone.isBlank()) return;
    boolean duplicatePhone =
        farmerId == null
            ? farmerRepository.existsByPhone(phone)
            : farmerRepository.existsByPhoneAndIdNot(phone, farmerId);
    if (duplicatePhone) throw new ConflictException("Farmer phone already exists");
  }

  private void syncLinkedUser(Farmer farmer) {
    if (farmer.getUserId() == null) return;
    User user =
        userRepository
            .findById(farmer.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Linked user not found"));
    if (userRepository.existsByPhoneAndIdNot(farmer.getPhone(), user.getId())) {
      throw new ConflictException("Farmer phone already has a user account");
    }
    user.setName(farmer.getName());
    user.setPhone(farmer.getPhone());
  }

  private Farmer saveAndFlushFarmer(Farmer farmer) {
    try {
      return farmerRepository.saveAndFlush(farmer);
    } catch (DataIntegrityViolationException exception) {
      String detail = exception.getMostSpecificCause().getMessage().toLowerCase(java.util.Locale.ROOT);
      if (detail.contains("farmer_code")) {
        throw new ConflictException("Farmer code already exists");
      }
      if (detail.contains("phone")) {
        throw new ConflictException("Farmer phone already exists");
      }
      if (detail.contains("user_id")) {
        throw new ConflictException("Farmer is already linked to another user");
      }
      throw exception;
    }
  }

  private void publishFarmerChanged(UUID farmerId) {
    domainEventPublisher.publishAfterCommit(
        new PublicTraceContentChangedEvent(
            PublicTraceContentChangedEvent.Scope.FARMER, farmerId));
  }

  private FarmerResponse saveUploadedFarmerFile(
      UUID farmerId, StoredFileResponse storedFile, java.util.function.Consumer<Farmer> update) {
    try {
      return transactionTemplate.execute(
          status -> {
            Farmer farmer = findFarmer(farmerId);
            update.accept(farmer);
            return FarmerResponse.from(farmerRepository.save(farmer), storageService);
          });
    } catch (RuntimeException exception) {
      deleteUploadedFileSafely(storedFile.fileKey());
      throw exception;
    }
  }

  private void deleteUploadedFileSafely(String fileKey) {
    try {
      storageService.delete(fileKey);
    } catch (RuntimeException ignored) {
      // Cleanup failure must not hide the database failure that triggered it.
    }
  }
}
