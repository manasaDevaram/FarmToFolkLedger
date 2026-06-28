package com.farmtofolk.farmtofolk_ledger.sales;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "batch_sale_transactions")
public class BatchSaleTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "quantity_sold", nullable = false)
    private BigDecimal quantitySold;

    @Column(name = "sale_price_per_unit", nullable = false)
    private BigDecimal salePricePerUnit;

    @Column(name = "sale_amount", nullable = false)
    private BigDecimal saleAmount;

    @Column(nullable = false)
    private String currency = "INR";

    @Column(name = "sold_at", nullable = false)
    private LocalDateTime soldAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (soldAt == null) soldAt = now;
        createdAt = now;
        currency = currency == null || currency.isBlank() ? "INR" : currency.trim().toUpperCase(Locale.ROOT);
        calculateSaleAmount();
    }

    public UUID getId() { return id; }
    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }
    public BigDecimal getQuantitySold() { return quantitySold; }
    public void setQuantitySold(BigDecimal quantitySold) { this.quantitySold = quantitySold; }
    public BigDecimal getSalePricePerUnit() { return salePricePerUnit; }
    public void setSalePricePerUnit(BigDecimal salePricePerUnit) { this.salePricePerUnit = salePricePerUnit; }
    public BigDecimal getSaleAmount() { return saleAmount; }
    public void calculateSaleAmount() { this.saleAmount = quantitySold.multiply(salePricePerUnit); }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) {
        this.currency = currency == null || currency.isBlank() ? "INR" : currency.trim().toUpperCase(Locale.ROOT);
    }
    public LocalDateTime getSoldAt() { return soldAt; }
    public void setSoldAt(LocalDateTime soldAt) { this.soldAt = soldAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
