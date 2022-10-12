import { NotificationStatus } from "./notificationStatus";
import { NotificationType } from "./notificationType";

export interface Notification {
    id: string;
    status: NotificationStatus;
    type: NotificationType;
    message: string;
    created: string;
    updated: string;
}