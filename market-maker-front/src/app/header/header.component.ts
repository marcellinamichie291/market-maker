import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../notification/notification.service';
import { NotificationStatus } from '../notification/notificationStatus';
import { Notification } from '../notification/notification';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  public orangeNotificationsCount: number | undefined;
  public redNotificationsCount: number | undefined;

  constructor(private notificationService: NotificationService) { }

  ngOnInit(): void {
    this.getNotifications(NotificationStatus.OPEN);
  }

  public getNotifications(status: NotificationStatus): void {
    this.notificationService.getNotifications(status).subscribe({
      next: (response: Notification[]) => {
        this.orangeNotificationsCount = response.filter(notification => notification.type === 'PLACE_ORDER_FAIL').length
        this.redNotificationsCount = response.filter(notification => notification.type === 'LOW_BALANCE').length
      },
      error: (error: HttpErrorResponse) => {
        alert(error.message);
      }
    });
  }

}
