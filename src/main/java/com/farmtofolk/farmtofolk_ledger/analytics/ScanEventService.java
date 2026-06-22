package com.farmtofolk.farmtofolk_ledger.analytics;

import com.farmtofolk.farmtofolk_ledger.qr.QrCode;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class ScanEventService {

    private final ScanEventRepository scanEventRepository;
    private final QrCodeRepository qrCodeRepository;

    public ScanEventService(ScanEventRepository scanEventRepository, QrCodeRepository qrCodeRepository) {
        this.scanEventRepository = scanEventRepository;
        this.qrCodeRepository = qrCodeRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ScanEventResponse recordScan(
            String publicToken,
            String country,
            String state,
            String city,
            String deviceType,
            String userAgent,
            String ipHash
    ) {
        // Resolve the public token to an active QR code before recording a scan.
        QrCode qrCode = qrCodeRepository.findByPublicTokenAndIsActiveTrue(publicToken)
                .orElseThrow(() -> new RuntimeException("QR code not found"));

        // Store only analytics metadata and the hashed IP value.
        ScanEvent scanEvent = new ScanEvent();
        scanEvent.setQrCodeId(qrCode.getId());
        scanEvent.setPublicToken(publicToken);
        scanEvent.setScannedAt(LocalDateTime.now());
        scanEvent.setCountry(country);
        scanEvent.setState(state);
        scanEvent.setCity(city);
        scanEvent.setDeviceType(deviceType);
        scanEvent.setUserAgent(userAgent);
        scanEvent.setIpHash(ipHash);

        ScanEvent savedScanEvent = scanEventRepository.save(scanEvent);
        return ScanEventResponse.from(savedScanEvent);
    }

    public long getScanCount(UUID qrCodeId) {
        // Count scans recorded against this QR code.
        return scanEventRepository.countByQrCodeId(qrCodeId);
    }
}
