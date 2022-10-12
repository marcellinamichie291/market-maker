import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "src/environments/environment";
import { Balance } from "./balance";
import { ExchangeName } from "../market-maker/exchangeName";

@Injectable({providedIn: 'root'})
export class BalanceService {
    private apiServerUrl = environment.apiBaseUrl;

    constructor(private http: HttpClient) { }

    public getBalance(exchangeName: ExchangeName): Observable<Balance[]> {
        let queryParams = new HttpParams();
        queryParams = queryParams.append("exchangeName", exchangeName);
        return this.http.get<Balance[]>(`${this.apiServerUrl}/balance`, {params:queryParams});
    }
}