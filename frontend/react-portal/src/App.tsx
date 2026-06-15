import { FormEvent, useEffect, useMemo, useState } from "react";
import { Check, Clock3, RefreshCcw, Search, Send, ShieldCheck, Waypoints } from "lucide-react";
import { api } from "./api";
import type { CreateTreasuryRequest, DashboardSummary, RequestStatus, RequestType, StatusEvent, TreasuryRequest } from "./types";

const requestTypes: RequestType[] = [
  "TREASURY_SERVICE_SETUP",
  "ACCOUNT_ACCESS_CHANGE",
  "ELIGIBILITY_CHECK",
  "WIRE_SETUP",
  "FX_PAYMENT",
  "REPORTING_CHANGE"
];

const initialForm: CreateTreasuryRequest = {
  clientName: "Acme Manufacturing",
  accountNumber: "782233100",
  requestType: "WIRE_SETUP",
  paymentAmount: 45000,
  paymentCurrency: "USD",
  createdBy: "banker",
  riskScore: 42,
  destinationSystem: "WIRE_PLATFORM"
};

const statusTone: Record<RequestStatus, string> = {
  RECEIVED: "status-neutral",
  ELIGIBILITY_REVIEW: "status-working",
  ELIGIBLE: "status-working",
  PENDING_APPROVAL: "status-warn",
  APPROVED: "status-working",
  SENT_TO_DOWNSTREAM: "status-working",
  COMPLETED: "status-good",
  REJECTED: "status-bad",
  FAILED: "status-bad",
  TIMED_OUT: "status-bad"
};

function App() {
  const [requests, setRequests] = useState<TreasuryRequest[]>([]);
  const [dashboard, setDashboard] = useState<DashboardSummary | null>(null);
  const [selectedId, setSelectedId] = useState<string>("");
  const [timeline, setTimeline] = useState<StatusEvent[]>([]);
  const [form, setForm] = useState<CreateTreasuryRequest>(initialForm);
  const [filter, setFilter] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const selected = useMemo(
    () => requests.find((request) => request.requestId === selectedId) ?? requests[0],
    [requests, selectedId]
  );

  const filtered = useMemo(() => {
    const value = filter.trim().toLowerCase();
    if (!value) {
      return requests;
    }
    return requests.filter((request) =>
      [request.requestId, request.clientName, request.status, request.requestType, request.currentStage ?? ""]
        .join(" ")
        .toLowerCase()
        .includes(value)
    );
  }, [filter, requests]);

  async function refresh() {
    setLoading(true);
    setError("");
    try {
      const [summary, list] = await Promise.all([api.dashboard(), api.requests()]);
      setDashboard(summary);
      setRequests(list);
      if (!selectedId && list.length > 0) {
        setSelectedId(list[0].requestId);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to refresh treasury requests");
    } finally {
      setLoading(false);
    }
  }

  async function loadTimeline(requestId: string) {
    try {
      setTimeline(await api.timeline(requestId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to load request timeline");
    }
  }

  async function createRequest(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      const created = await api.create(form);
      setSelectedId(created.requestId);
      await refresh();
      await loadTimeline(created.requestId);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to create treasury request");
    } finally {
      setLoading(false);
    }
  }

  async function approveSelected() {
    if (!selected) {
      return;
    }
    setLoading(true);
    setError("");
    try {
      await api.approve(selected.requestId);
      await refresh();
      await loadTimeline(selected.requestId);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to approve request");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refresh();
    const timer = window.setInterval(refresh, 5000);
    return () => window.clearInterval(timer);
  }, []);

  useEffect(() => {
    if (selected?.requestId) {
      loadTimeline(selected.requestId);
    }
  }, [selected?.requestId]);

  return (
    <main className="app-shell">
      <section className="topbar">
        <div>
          <p className="eyebrow">Commercial treasury operations</p>
          <h1>Request Workflow Console</h1>
        </div>
        <button className="icon-button" onClick={refresh} disabled={loading} title="Refresh">
          <RefreshCcw size={18} />
        </button>
      </section>

      {error && <div className="alert alert-danger py-2">{error}</div>}

      <section className="metrics-grid">
        <Metric icon={<Waypoints size={18} />} label="Total" value={dashboard?.totalRequests ?? 0} />
        <Metric icon={<Clock3 size={18} />} label="Pending" value={dashboard?.byStatus?.PENDING_APPROVAL ?? 0} />
        <Metric icon={<ShieldCheck size={18} />} label="Completed" value={dashboard?.byStatus?.COMPLETED ?? 0} />
        <Metric icon={<Check size={18} />} label="Rejected" value={dashboard?.byStatus?.REJECTED ?? 0} />
      </section>

      <section className="workspace-grid">
        <form className="panel" onSubmit={createRequest}>
          <div className="panel-heading">
            <h2>Create Request</h2>
            <button className="primary-button" type="submit" disabled={loading}>
              <Send size={16} />
              Submit
            </button>
          </div>

          <label>
            Client
            <input
              className="form-control"
              value={form.clientName}
              onChange={(event) => setForm({ ...form, clientName: event.target.value })}
            />
          </label>

          <label>
            Account
            <input
              className="form-control"
              value={form.accountNumber}
              onChange={(event) => setForm({ ...form, accountNumber: event.target.value })}
            />
          </label>

          <label>
            Request type
            <select
              className="form-select"
              value={form.requestType}
              onChange={(event) => setForm({ ...form, requestType: event.target.value as RequestType })}
            >
              {requestTypes.map((type) => (
                <option key={type} value={type}>
                  {type.replaceAll("_", " ")}
                </option>
              ))}
            </select>
          </label>

          <div className="two-col">
            <label>
              Amount
              <input
                className="form-control"
                type="number"
                value={form.paymentAmount}
                onChange={(event) => setForm({ ...form, paymentAmount: Number(event.target.value) })}
              />
            </label>
            <label>
              Currency
              <select
                className="form-select"
                value={form.paymentCurrency}
                onChange={(event) => setForm({ ...form, paymentCurrency: event.target.value })}
              >
                <option>USD</option>
                <option>EUR</option>
                <option>GBP</option>
              </select>
            </label>
          </div>

          <div className="two-col">
            <label>
              Risk score
              <input
                className="form-control"
                type="number"
                min="0"
                max="100"
                value={form.riskScore}
                onChange={(event) => setForm({ ...form, riskScore: Number(event.target.value) })}
              />
            </label>
            <label>
              Destination
              <input
                className="form-control"
                value={form.destinationSystem}
                onChange={(event) => setForm({ ...form, destinationSystem: event.target.value })}
              />
            </label>
          </div>
        </form>

        <section className="panel request-list">
          <div className="panel-heading">
            <h2>Requests</h2>
            <div className="search-box">
              <Search size={16} />
              <input value={filter} onChange={(event) => setFilter(event.target.value)} placeholder="Search" />
            </div>
          </div>

          <div className="table-wrap">
            <table className="table align-middle">
              <thead>
                <tr>
                  <th>Request ID</th>
                  <th>Client</th>
                  <th>Type</th>
                  <th>Status</th>
                  <th>Stage</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((request) => (
                  <tr
                    key={request.requestId}
                    className={request.requestId === selected?.requestId ? "selected-row" : ""}
                    onClick={() => setSelectedId(request.requestId)}
                  >
                    <td className="request-id">{request.requestId}</td>
                    <td>{request.clientName}</td>
                    <td>{request.requestType.replaceAll("_", " ")}</td>
                    <td>
                      <span className={`status-pill ${statusTone[request.status]}`}>{request.status.replaceAll("_", " ")}</span>
                    </td>
                    <td>{request.currentStage}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className="panel detail-panel">
          <div className="panel-heading">
            <h2>Trace</h2>
            <button
              className="secondary-button"
              onClick={approveSelected}
              disabled={loading || selected?.status !== "PENDING_APPROVAL"}
              title="Approve"
            >
              <Check size={16} />
              Approve
            </button>
          </div>

          {selected ? (
            <>
              <div className="detail-grid">
                <div>
                  <span>Request</span>
                  <strong>{selected.requestId}</strong>
                </div>
                <div>
                  <span>System</span>
                  <strong>{selected.assignedSystem ?? "Unassigned"}</strong>
                </div>
                <div>
                  <span>Status reason</span>
                  <strong>{selected.statusReason ?? "Queued"}</strong>
                </div>
              </div>

              <ol className="timeline">
                {timeline.map((event) => (
                  <li key={event.id}>
                    <div className={`timeline-dot ${statusTone[event.status]}`} />
                    <div>
                      <strong>{event.status.replaceAll("_", " ")}</strong>
                      <span>{event.stage} by {event.actor}</span>
                      <p>{event.message}</p>
                    </div>
                  </li>
                ))}
              </ol>
            </>
          ) : (
            <div className="empty-state">No requests yet.</div>
          )}
        </section>
      </section>
    </main>
  );
}

function Metric({ icon, label, value }: { icon: JSX.Element; label: string; value: number }) {
  return (
    <div className="metric">
      <div className="metric-icon">{icon}</div>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

export default App;
