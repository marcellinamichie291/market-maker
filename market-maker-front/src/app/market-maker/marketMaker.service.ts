import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { MarketMaker } from "./marketMaker";
import { MarketMakerRequest } from "./marketMakerRequest";  
import { MarketMakerShortRequest } from "./marketMakerShortRequest";
import { environment } from "src/environments/environment";
import { MarketMakerStatus } from "./marketMakerStatus";

@Injectable({providedIn: 'root'})
export class MarketMakerService {
    private apiServerUrl = environment.apiBaseUrl;

    constructor(private http: HttpClient) { }

    public getMarketMakers(status: MarketMakerStatus): Observable<MarketMaker[]> {
        let queryParams = new HttpParams();
        queryParams = queryParams.append("status", status);
        return this.http.get<MarketMaker[]>(`${this.apiServerUrl}/market-maker`, {params:queryParams});
    }

    public createMarketMaker(marketMakerRequest: MarketMakerRequest): Observable<void> {
        return this.http.post<void>(`${this.apiServerUrl}/market-maker`, marketMakerRequest);
    }

    public stopMarketMaker(marketMakerShortRequest: MarketMakerShortRequest): Observable<void> {
        return this.http.put<void>(`${this.apiServerUrl}/market-maker`, marketMakerShortRequest);
    }
}