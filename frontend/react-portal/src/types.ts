export type RequestStatus =
  | "RECEIVED"
  | "ELIGIBILITY_REVIEW"
  | "ELIGIBLE"
  | "PENDING_APPROVAL"
  | "APPROVED"
  | "SENT_TO_DOWNSTREAM"
  | "COMPLETED"
  | "REJECTED"
  | "FAILED"
  | "TIMED_OUT";

export type RequestType =
  | "TREASURY_SERVICE_SETUP"
  | "ACCOUNT_ACCESS_CHANGE"
  | "ELIGIBILITY_CHECK"
  | "WIRE_SETUP"
  | "FX_PAYMENT"
  | "REPORTING_CHANGE";

export interface TreasuryRequest {
  requestId: string;
  clientName: string;
  accountNumber?: string;
  requestType: RequestType;
  status: RequestStatus;
  currentStage?: string;
  assignedSystem?: string;
  destinationSystem?: string;
  paymentAmount?: number;
  paymentCurrency?: string;
  riskScore?: number;
  createdBy: string;
  approvedBy?: string;
  statusReason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface StatusEvent {
  id: number;
  requestId: string;
  status: RequestStatus;
  stage?: string;
  actor?: string;
  message?: string;
  createdAt: string;
}

export interface DashboardSummary {
  totalRequests: number;
  byStatus: Record<RequestStatus, number>;
}

export interface CreateTreasuryRequest {
  clientName: string;
  accountNumber: string;
  requestType: RequestType;
  paymentAmount: number;
  paymentCurrency: string;
  createdBy: string;
  riskScore: number;
  destinationSystem: string;
}
