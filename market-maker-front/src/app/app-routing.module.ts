import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MarketMakerComponent } from './market-maker/market-maker.component';
import { NotificationComponent } from './notification/notification.component';
import { BalanceComponent } from './balance/balance.component';

const routes: Routes = [
  {
    path: '',
    component: MarketMakerComponent
  },
  {
    path: 'notification',
    component: NotificationComponent
  },
  {
    path: 'balance',
    component: BalanceComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
