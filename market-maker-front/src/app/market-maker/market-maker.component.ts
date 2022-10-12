import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { CurrencyPair, CurrencyPairToIconMapping } from './currencyPair';
import { ExchangeName } from './exchangeName';
import { MarketMaker } from './marketMaker';
import { MarketMakerService } from './marketMaker.service';
import { MarketMakerShortRequest } from './marketMakerShortRequest';
import { MarketMakerStatus } from './marketMakerStatus';

@Component({
  selector: 'app-market-maker',
  templateUrl: './market-maker.component.html',
  styleUrls: ['./market-maker.component.css']
})
export class MarketMakerComponent implements OnInit {
  public exchangeNames = Object.values(ExchangeName);
  public currencyPairs = Object.values(CurrencyPair);
  public CurrencyPairToIconMapping = CurrencyPairToIconMapping;
  public marketMakers: MarketMaker[] = [];
  public updateMarketMaker: MarketMaker | undefined;
  public stopMarketMaker: MarketMaker | undefined;

  constructor(private marketMakerService: MarketMakerService) { }

  ngOnInit(): void {
    this.getMarketMakers(MarketMakerStatus.WORKING);
  }

  public getMarketMakers(status: MarketMakerStatus): void {
    this.marketMakerService.getMarketMakers(status).subscribe({
      next: (response: MarketMaker[]) => {
        this.marketMakers = response;
      },
      error: (error: HttpErrorResponse) => {
        alert(error.message);
      }
    })
  }

  public getArchivedMarketMakers(): void {
    this.getMarketMakers(MarketMakerStatus.STOPPED);
  }

  public onCreateMarketMaker(createForm: NgForm): void {
    const element = document.getElementById('create-market-maker-form') as HTMLElement;
    element.click();
    this.marketMakerService.createMarketMaker(createForm.value).subscribe({
      next: (response: void) => {
        this.getMarketMakers(MarketMakerStatus.WORKING);
        createForm.reset();
      },
      error: (error: HttpErrorResponse) => {
        alert(error.message);
        createForm.reset();
      }
    });
  }

  public onUpdateMarketMaker(updateForm: NgForm): void {
    const element = document.getElementById('update-market-maker-form') as HTMLElement;
    element.click();
    this.marketMakerService.createMarketMaker(updateForm.value).subscribe({
      next: (response: void) => {
        this.getMarketMakers(MarketMakerStatus.WORKING);
        updateForm.reset();
      },
      error: (error: HttpErrorResponse) => {
        alert(error.message);
      }
    });
  }

  public onStopMarketMaker(marketMaker: MarketMaker, stopForm: NgForm): void {
    const element = document.getElementById('stop-market-maker-form') as HTMLElement;
    element.click();
    let marketMakerShortRequest: MarketMakerShortRequest = {
      exchangeName: marketMaker?.exchangeName,
      currencyPair: marketMaker?.currencyPair,
      needCancelOrders: stopForm.value.needCancelOrders
    };
    this.marketMakerService.stopMarketMaker(marketMakerShortRequest).subscribe({
      next: (response: void) => {
        this.getMarketMakers(MarketMakerStatus.WORKING);
      },
      error: (error: HttpErrorResponse) => {
        alert(error.message);
      }
    });
  }

  public onOpenModal(marketMaker: MarketMaker, mode: string): void {
    const container = document.getElementById('main-container') as HTMLElement;
    const button = document.createElement('button');
    button.type = 'button';
    button.style.display = 'none';
    button.setAttribute('data-toggle', 'modal');
    if (mode === "create") {
      button.setAttribute('data-target', '#createMarketMakerModal');
    }
    if (mode === "update") {
      this.updateMarketMaker = marketMaker;
      button.setAttribute('data-target', '#updateMarketMakerModal');
    }
    if (mode === "stop") {
      this.stopMarketMaker = marketMaker;
      button.setAttribute('data-target', '#stopMarketMakerModal');
    }
    container.appendChild(button);
    button.click();
  }
}
