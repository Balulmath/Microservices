import { bootstrapApplication } from "@angular/platform-browser";
import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { HttpClient, HttpClientModule, HttpHeaders } from "@angular/common/http";

interface TreasuryRequest {
  requestId: string;
  clientName: string;
  requestType: string;
  status: string;
  currentStage?: string;
  statusReason?: string;
}

@Component({
  selector: "app-root",
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: "./app/app.component.html"
})
class AppComponent {
  requests: TreasuryRequest[] = [];
  selected?: TreasuryRequest;
  loading = false;
  error = "";

  private readonly apiBase = "http://localhost:8080";
  private readonly headers = new HttpHeaders({
    Authorization: `Basic ${btoa("operations:ops123")}`
  });

  constructor(private readonly http: HttpClient) {
    this.refresh();
  }

  refresh() {
    this.loading = true;
    this.error = "";
    this.http.get<TreasuryRequest[]>(`${this.apiBase}/api/treasury/requests`, { headers: this.headers }).subscribe({
      next: (requests) => {
        this.requests = requests;
        this.selected = this.selected ?? requests[0];
        this.loading = false;
      },
      error: () => {
        this.error = "Unable to load treasury requests";
        this.loading = false;
      }
    });
  }

  select(request: TreasuryRequest) {
    this.selected = request;
  }
}

bootstrapApplication(AppComponent).catch((error) => console.error(error));
