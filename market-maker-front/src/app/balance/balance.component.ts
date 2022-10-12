import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ExchangeName } from '../market-maker/exchangeName';
import { Balance } from './balance';
import { BalanceService } from './balance.service';

@Component({
  selector: 'app-balance',
  templateUrl: './balance.component.html',
  styleUrls: ['./balance.component.css']
})
export class BalanceComponent implements OnInit {
  public balances: Balance[] = [];

  constructor(private balanceService: BalanceService) { }

  ngOnInit(): void {
    this.getBalance(ExchangeName.BITMART);
  }

  public getBalance(exchangeName: ExchangeName): void {
    this.balanceService.getBalance(exchangeName).subscribe({
      next: (response: Balance[]) => {
        this.balances = response;
      },
      error: (error: HttpErrorResponse) => {
        alert(error.message);
      }
    })
  }

}
