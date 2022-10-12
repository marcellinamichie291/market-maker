import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Notification } from './notification';
import { NotificationService } from './notification.service';
import { NotificationStatus } from './notificationStatus';

@Component({
  selector: 'app-notification',
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.css']
})
export class NotificationComponent implements OnInit {
  public notifications: Notification[] = [];

  constructor(private notificationService: NotificationService) { }

  ngOnInit(): void {
    this.getNotifications(NotificationStatus.OPEN);
  }

  public getNotifications(status: NotificationStatus): void {
    this.notificationService.getNotifications(status).subscribe({
      next: (response: Notification[]) => {
        this.notifications = response;
      },
      error: (error: HttpErrorResponse) => {
        alert(error.message);
      }
    });
  }

  public getArchivedNotifications(): void {
    this.getNotifications(NotificationStatus.CLOSED);
  }

  public onClose(notificationId: string): void {
    this.notificationService.closeNotification(notificationId).subscribe({
      next: (response: void) => {
        this.getNotifications(NotificationStatus.OPEN);
      },
      error: (error: HttpErrorResponse) => {
        alert(error.message);
      }
    });
  }
}
