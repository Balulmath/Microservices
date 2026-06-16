import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  Check,
  Clock3,
  LogIn,
  LogOut,
  RefreshCcw,
  Search,
  Send,
  ShieldCheck,
  UserCog,
  Users,
  Waypoints,
  XCircle
} from "lucide-react";
import { api } from "./api";
import type {
  AuthSession,
  CreateTreasuryRequest,
  DashboardSummary,
  RequestStatus,
  RequestType,
  StatusEvent,
  TreasuryRequest,
  UserRole
} from "./types";

const requestTypes: RequestType[] = [
  "TREASURY_SERVICE_SETUP",
  "ACCOUNT_ACCESS_CHANGE",
  "ELIGIBILITY_CHECK",
  "WIRE_SETUP",
  "FX_PAYMENT",
  "REPORTING_CHANGE"
];

const demoUsers: AuthSession[] = [
  {
    username: "banker",
    password: "banker123",
    role: "BANKER",
    portalTitle: "Banker Request Portal"
  },
  {
    username: "manager",
    password: "manager123",
    role: "MANAGER",
    portalTitle: "Manager Approval Portal"
  },
  {
    username: "operations",
    password: "ops123",
    role: "OPERATIONS",
    portalTitle: "Operations Monitor"
  },
  {
    username: "admin",
    password: "admin123",
    role: "ADMIN",
    portalTitle: "Admin Control Portal"
  }
];

const roleSummary: Record<UserRole, string> = {
  BANKER: "Create requests and track status",
  MANAGER: "Approve high-value and FX requests",
  OPERATIONS: "Monitor and mark operational failures",
  ADMIN: "Full workflow control"
};

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
  const [session, setSession] = useState<AuthSession | null>(null);
  const [loginForm, setLoginForm] = useState({ username: "banker", password: "banker123" });
  const [requests, setRequests] = useState<TreasuryRequest[]>([]);
  const [dashboard, setDashboard] = useState<DashboardSummary | null>(null);
  const [selectedId, setSelectedId] = useState("");
  const [timeline, setTimeline] = useState<StatusEvent[]>([]);
  const [form, setForm] = useState<CreateTreasuryRequest>(initialForm);
  const [filter, setFilter] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const canCreate = session?.role === "BANKER" || session?.role === "ADMIN";
  const canApprove = session?.role === "MANAGER" || session?.role === "ADMIN";
  const canFail = session?.role === "OPERATIONS" || session?.role === "ADMIN";

  const visibleRequests = useMemo(() => {
    const scoped =
      session?.role === "BANKER"
        ? requests.filter((request) => request.createdBy === session.username)
        : requests;
    const value = filter.trim().toLowerCase();
    if (!value) {
      return scoped;
    }
    return scoped.filter((request) =>
      [request.requestId, request.clientName, request.status, request.requestType, request.currentStage ?? ""]
        .join(" ")
        .toLowerCase()
        .includes(value)
    );
  }, [filter, requests, session]);

  const selected = useMemo(
    () => visibleRequests.find((request) => request.requestId === selectedId) ?? visibleRequests[0],
    [visibleRequests, selectedId]
  );

  const pendingApprovalCount = dashboard?.byStatus?.PENDING_APPROVAL ?? 0;
  const selectedIsTerminal =
    selected?.status === "COMPLETED" ||
    selected?.status === "REJECTED" ||
    selected?.status === "FAILED" ||
    selected?.status === "TIMED_OUT";

  async function login(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const account = demoUsers.find(
      (user) => user.username === loginForm.username.trim() && user.password === loginForm.password
    );
    if (!account) {
      setError("Invalid demo credentials");
      return;
    }
    setLoading(true);
    setError("");
    try {
      await api.dashboard(account);
      setSession(account);
      setForm({ ...initialForm, createdBy: account.username });
      await refresh(account);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to sign in");
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    setSession(null);
    setRequests([]);
    setDashboard(null);
    setSelectedId("");
    setTimeline([]);
    setError("");
  }

  async function refresh(activeSession = session) {
    if (!activeSession) {
      return;
    }
    setLoading(true);
    setError("");
    try {
      const [summary, list] = await Promise.all([api.dashboard(activeSession), api.requests(activeSession)]);
      const sorted = sortForPortal(list, activeSession.role);
      setDashboard(summary);
      setRequests(sorted);
      if (sorted.length === 0) {
        setSelectedId("");
      } else if (!selectedId || !sorted.some((request) => request.requestId === selectedId)) {
        setSelectedId(sorted[0].requestId);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to refresh treasury requests");
    } finally {
      setLoading(false);
    }
  }

  async function loadTimeline(requestId: string, activeSession = session) {
    if (!activeSession) {
      return;
    }
    try {
      setTimeline(await api.timeline(activeSession, requestId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to load request timeline");
    }
  }

  async function createRequest(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!session) {
      return;
    }
    setLoading(true);
    setError("");
    try {
      const created = await api.create(session, { ...form, createdBy: session.username });
      setSelectedId(created.requestId);
      await refresh(session);
      await loadTimeline(created.requestId, session);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to create treasury request");
    } finally {
      setLoading(false);
    }
  }

  async function approveSelected() {
    if (!session || !selected) {
      return;
    }
    setLoading(true);
    setError("");
    try {
      await api.approve(session, selected.requestId);
      await refresh(session);
      await loadTimeline(selected.requestId, session);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to approve request");
    } finally {
      setLoading(false);
    }
  }

  async function failSelected() {
    if (!session || !selected) {
      return;
    }
    setLoading(true);
    setError("");
    try {
      await api.fail(session, selected.requestId);
      await refresh(session);
      await loadTimeline(selected.requestId, session);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to mark request failed");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!session) {
      return undefined;
    }
    refresh(session);
    const timer = window.setInterval(() => refresh(session), 5000);
    return () => window.clearInterval(timer);
  }, [session?.username]);

  useEffect(() => {
    if (session && selected?.requestId) {
      loadTimeline(selected.requestId, session);
    } else {
      setTimeline([]);
    }
  }, [session?.username, selected?.requestId]);

  if (!session) {
    return (
      <main className="login-shell">
        <section className="login-panel">
          <div>
            <p className="eyebrow">Commercial treasury operations</p>
            <h1>Treasury Workflow Access</h1>
          </div>

          {error && <div className="alert alert-danger py-2">{error}</div>}

          <form className="login-form" onSubmit={login}>
            <label>
              Username
              <input
                className="form-control"
                value={loginForm.username}
                onChange={(event) => setLoginForm({ ...loginForm, username: event.target.value })}
              />
            </label>
            <label>
              Password
              <input
                className="form-control"
                type="password"
                value={loginForm.password}
                onChange={(event) => setLoginForm({ ...loginForm, password: event.target.value })}
              />
            </label>
            <button className="primary-button" type="submit" disabled={loading}>
              <LogIn size={16} />
              Sign in
            </button>
          </form>

          <div className="role-picks">
            {demoUsers.map((user) => (
              <button
                className="role-pick"
                key={user.username}
                type="button"
                onClick={() => setLoginForm({ username: user.username, password: user.password })}
              >
                <UserCog size={18} />
                <strong>{user.portalTitle}</strong>
                <span>{user.username} / {user.password}</span>
              </button>
            ))}
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="app-shell">
      <section className="topbar">
        <div>
          <p className="eyebrow">Commercial treasury operations</p>
          <h1>{session.portalTitle}</h1>
        </div>
        <div className="topbar-actions">
          <span className="role-badge">
            <Users size={15} />
            {session.username} / {session.role}
          </span>
          <button className="icon-button" onClick={() => refresh(session)} disabled={loading} title="Refresh">
            <RefreshCcw size={18} />
          </button>
          <button className="icon-button" onClick={logout} title="Sign out">
            <LogOut size={18} />
          </button>
        </div>
      </section>

      {error && <div className="alert alert-danger py-2">{error}</div>}

      <section className="metrics-grid">
        <Metric icon={<Waypoints size={18} />} label="Total" value={dashboard?.totalRequests ?? 0} />
        <Metric icon={<Clock3 size={18} />} label="Pending" value={pendingApprovalCount} />
        <Metric icon={<ShieldCheck size={18} />} label="Completed" value={dashboard?.byStatus?.COMPLETED ?? 0} />
        <Metric icon={<XCircle size={18} />} label="Rejected" value={dashboard?.byStatus?.REJECTED ?? 0} />
      </section>

      <section className="workspace-grid">
        {canCreate ? (
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
        ) : (
          <section className="panel role-panel">
            <div className="panel-heading">
              <h2>{session.role === "MANAGER" ? "Approval Queue" : "Role Actions"}</h2>
              <span className="mini-badge">{session.role}</span>
            </div>
            <div className="queue-summary">
              <span>{roleSummary[session.role]}</span>
              <strong>{pendingApprovalCount}</strong>
              <span>requests waiting for approval</span>
            </div>
            {canApprove && (
              <button
                className="primary-button full-width"
                type="button"
                onClick={approveSelected}
                disabled={loading || selected?.status !== "PENDING_APPROVAL"}
              >
                <Check size={16} />
                Approve Selected
              </button>
            )}
            {canFail && (
              <button
                className="danger-button full-width"
                type="button"
                onClick={failSelected}
                disabled={loading || !selected || selectedIsTerminal}
              >
                <XCircle size={16} />
                Mark Failed
              </button>
            )}
          </section>
        )}

        <section className="panel request-list">
          <div className="panel-heading">
            <h2>{session.role === "MANAGER" ? "Approval Requests" : "Requests"}</h2>
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
                {visibleRequests.map((request) => (
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
            <div className="action-row">
              {canApprove && (
                <button
                  className="secondary-button"
                  onClick={approveSelected}
                  disabled={loading || selected?.status !== "PENDING_APPROVAL"}
                  title="Approve"
                >
                  <Check size={16} />
                  Approve
                </button>
              )}
              {canFail && (
                <button
                  className="danger-button"
                  onClick={failSelected}
                  disabled={loading || !selected || selectedIsTerminal}
                  title="Mark failed"
                >
                  <XCircle size={16} />
                  Fail
                </button>
              )}
            </div>
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
                  <span>Created by</span>
                  <strong>{selected.createdBy}</strong>
                </div>
                <div>
                  <span>Approved by</span>
                  <strong>{selected.approvedBy ?? "Not approved"}</strong>
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

function sortForPortal(requests: TreasuryRequest[], role: UserRole) {
  return [...requests].sort((left, right) => {
    if (role === "MANAGER") {
      const leftPending = left.status === "PENDING_APPROVAL" ? 0 : 1;
      const rightPending = right.status === "PENDING_APPROVAL" ? 0 : 1;
      if (leftPending !== rightPending) {
        return leftPending - rightPending;
      }
    }
    return right.updatedAt.localeCompare(left.updatedAt);
  });
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
