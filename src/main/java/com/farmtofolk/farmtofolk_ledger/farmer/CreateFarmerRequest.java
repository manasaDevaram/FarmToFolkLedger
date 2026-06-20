package com.farmtofolk.farmtofolk_ledger.farmer;

import java.time.LocalDate;

public record CreateFarmerRequest(
        String farmerCode,
        String name,
        String phone,
        String village,
        String district,
        String state,
        String bio,
        String profilePhotoUrl,
        String introVideoUrl,
        LocalDate joinedDate
) {
}
