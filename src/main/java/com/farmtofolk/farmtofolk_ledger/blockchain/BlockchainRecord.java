package com.farmtofolk.farmtofolk_ledger.blockchain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "blockchain_records")
public class BlockchainRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "entity_type", nullable = false)
  private String entityType;

  @Column(name = "entity_id", nullable = false)
  private UUID entityId;

  @Column(name = "record_hash", nullable = false)
  private String recordHash;

  @Column(name = "transaction_hash")
  private String transactionHash;

  private String network;

  private String status;

  @Column(name = "anchored_at")
  private LocalDateTime anchoredAt;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  void prePersist() {
    createdAt = LocalDateTime.now();
  }

  public UUID getId() {
    return id;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public UUID getEntityId() {
    return entityId;
  }

  public void setEntityId(UUID entityId) {
    this.entityId = entityId;
  }

  public String getRecordHash() {
    return recordHash;
  }

  public void setRecordHash(String recordHash) {
    this.recordHash = recordHash;
  }

  public String getTransactionHash() {
    return transactionHash;
  }

  public void setTransactionHash(String transactionHash) {
    this.transactionHash = transactionHash;
  }

  public String getNetwork() {
    return network;
  }

  public void setNetwork(String network) {
    this.network = network;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getAnchoredAt() {
    return anchoredAt;
  }

  public void setAnchoredAt(LocalDateTime anchoredAt) {
    this.anchoredAt = anchoredAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
