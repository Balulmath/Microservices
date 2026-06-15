import type { CreateTreasuryRequest, DashboardSummary, StatusEvent, TreasuryRequest } from "./types";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const BASIC_AUTH = `Basic ${btoa("banker:banker123")}`;

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      Authorization: BASIC_AUTH,
      ...(options.headers ?? {})
    }
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed with ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export const api = {
  dashboard: () => request<DashboardSummary>("/api/treasury/requests/dashboard"),
  requests: () => request<TreasuryRequest[]>("/api/treasury/requests"),
  timeline: (requestId: string) => request<StatusEvent[]>(`/api/treasury/requests/${requestId}/timeline`),
  create: (payload: CreateTreasuryRequest) =>
    request<TreasuryRequest>("/api/treasury/requests", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  approve: (requestId: string) =>
    request<TreasuryRequest>(`/api/treasury/requests/${requestId}/approve`, {
      method: "POST",
      body: JSON.stringify({
        actor: "banker",
        reason: "Approved from React treasury portal"
      })
    })
};
