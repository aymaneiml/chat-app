import { Injectable } from '@angular/core';
import { StompSubscription ,IMessage} from '@stomp/stompjs';
import { Subject } from 'rxjs';
import { ChatMessage, ChatMessageRequest } from '../../models/chat-message.model';
import { WebSocketConnectionService } from '../ws/web-socket-connection-service';

@Injectable({
  providedIn: 'root',
})
export class ChatService {

  private currentSubscription: StompSubscription | null = null;
  private messagesSubject = new Subject<ChatMessage>();
  
  messages$ = this.messagesSubject.asObservable();

  constructor(private wsConnection: WebSocketConnectionService) {
    this.wsConnection.connect();
  }

  joinRoom(roomId: string, sender: string): void {
    this.currentSubscription?.unsubscribe();

    this.currentSubscription = this.wsConnection.subscribe(
      `/topic/room.${roomId}`,
      (message: IMessage) => this.handleIncomingMessage(message)
    );

    const request: ChatMessageRequest = { sender, content: '' };
    this.wsConnection.publish(`/app/chat.join/${roomId}`, request);
  }

  sendMessage(roomId: string, sender: string, content: string): void {
    const request: ChatMessageRequest = { sender, content };
    this.wsConnection.publish(`/app/chat.sendMessage/${roomId}`, request);
  }

  leaveRoom(): void {
    this.currentSubscription?.unsubscribe();
    this.currentSubscription = null;
  }

  private handleIncomingMessage(message: IMessage): void {
    const parsed: ChatMessage = JSON.parse(message.body) as ChatMessage;
    this.messagesSubject.next(parsed);
  }
}
