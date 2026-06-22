package com.farmtofolk.farmtofolk_ledger.pricing;

import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class PriceBreakdownService {

    private final PriceBreakdownRepository priceBreakdownRepository;
    private final BatchRepository batchRepository;

    public PriceBreakdownService(
            PriceBreakdownRepository priceBreakdownRepository,
            BatchRepository batchRepository
    ) {
        this.priceBreakdownRepository = priceBreakdownRepository;
        this.batchRepository = batchRepository;
    }

    public PriceBreakdownResponse createPriceBreakdown(UUID batchId, CreatePriceBreakdownRequest request) {
        // Make sure the price breakdown is linked to a real batch.
        verifyBatchExists(batchId);
        // Allow only one price breakdown per batch.
        verifyPriceBreakdownDoesNotExist(batchId);

        // Copy request data into a new PriceBreakdown entity.
        PriceBreakdown priceBreakdown = new PriceBreakdown();
        priceBreakdown.setBatchId(batchId);
        applyRequest(priceBreakdown, request);

        // Save the price breakdown and return API-friendly response data.
        PriceBreakdown savedPriceBreakdown = priceBreakdownRepository.save(priceBreakdown);
        return PriceBreakdownResponse.from(savedPriceBreakdown);
    }

    public PriceBreakdownResponse getPriceBreakdown(UUID batchId) {
        // Load the price breakdown for this batch and convert it to a response.
        PriceBreakdown priceBreakdown = findPriceBreakdown(batchId);
        return PriceBreakdownResponse.from(priceBreakdown);
    }

    public PriceBreakdownResponse updatePriceBreakdown(UUID batchId, CreatePriceBreakdownRequest request) {
        // Make sure the batch still exists before updating its price breakdown.
        verifyBatchExists(batchId);

        // Load the existing price breakdown, update its fields, then save it.
        PriceBreakdown priceBreakdown = findPriceBreakdown(batchId);
        applyRequest(priceBreakdown, request);

        PriceBreakdown savedPriceBreakdown = priceBreakdownRepository.save(priceBreakdown);
        return PriceBreakdownResponse.from(savedPriceBreakdown);
    }

    private PriceBreakdown findPriceBreakdown(UUID batchId) {
        // Reuse one not-found lookup rule for price breakdown reads and updates.
        return priceBreakdownRepository.findByBatchId(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Price breakdown not found"));
    }

    private void verifyPriceBreakdownDoesNotExist(UUID batchId) {
        // Prevent creating a second price breakdown for the same batch.
        if (priceBreakdownRepository.findByBatchId(batchId).isPresent()) {
            throw new ConflictException("Price breakdown already exists");
        }
    }

    private void verifyBatchExists(UUID batchId) {
        // Prevent creating or updating price breakdowns for batches that do not exist.
        if (!batchRepository.existsById(batchId)) {
            throw new ResourceNotFoundException("Batch not found");
        }
    }

    private void applyRequest(PriceBreakdown priceBreakdown, CreatePriceBreakdownRequest request) {
        // Keep request-to-entity field mapping in one place.
        priceBreakdown.setConsumerPrice(request.consumerPrice());
        priceBreakdown.setFarmerPrice(request.farmerPrice());
        priceBreakdown.setTransportCost(request.transportCost());
        priceBreakdown.setPackingCost(request.packingCost());
        priceBreakdown.setOrganizationCost(request.organizationCost());
        priceBreakdown.setPlatformCost(request.platformCost());
        priceBreakdown.setCurrency(request.currency());
        priceBreakdown.setPriceUnit(request.priceUnit());
    }
}
