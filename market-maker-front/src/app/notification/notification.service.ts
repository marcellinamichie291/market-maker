import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "src/environments/environment";
import { NotificationStatus } from "./notificationStatus";
import { Notification } from "./notification";

@Injectable({providedIn: 'root'})
export class NotificationService {
    private apiServerUrl = environment.apiBaseUrl;

    constructor(private http: HttpClient) { }

    public getNotifications(status: NotificationStatus): Observable<Notification[]> {
        let queryParams = new HttpParams();
        queryParams = queryParams.append("status", status);
        return this.http.get<Notification[]>(`${this.apiServerUrl}/notification`, {params:queryParams});
    }

    public closeNotification(notificationId: string): Observable<void> {
        return this.http.put<void>(`${this.apiServerUrl}/notification/${notificationId}`, null);
    }
}