import type { AuthSession, CreateTreasuryRequest, DashboardSummary, StatusEvent, TreasuryRequest } from "./types";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

function basicAuth(session: AuthSession) {
  return `Basic ${btoa(`${session.username}:${session.password}`)}`;
}

async function request<T>(session: AuthSession, path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      Authorization: basicAuth(session),
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
  dashboard: (session: AuthSession) => request<DashboardSummary>(session, "/api/treasury/requests/dashboard"),
  requests: (session: AuthSession) => request<TreasuryRequest[]>(session, "/api/treasury/requests"),
  timeline: (session: AuthSession, requestId: string) =>
    request<StatusEvent[]>(session, `/api/treasury/requests/${requestId}/timeline`),
  create: (session: AuthSession, payload: CreateTreasuryRequest) =>
    request<TreasuryRequest>(session, "/api/treasury/requests", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  approve: (session: AuthSession, requestId: string) =>
    request<TreasuryRequest>(session, `/api/treasury/requests/${requestId}/approve`, {
      method: "POST",
      body: JSON.stringify({
        actor: session.username,
        reason: `Approved from ${session.portalTitle}`
      })
    }),
  fail: (session: AuthSession, requestId: string) =>
    request<TreasuryRequest>(session, `/api/treasury/requests/${requestId}/fail`, {
      method: "POST",
      body: JSON.stringify({
        actor: session.username,
        reason: `Marked failed from ${session.portalTitle}`
      })
    })
};
