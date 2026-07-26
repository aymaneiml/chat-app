import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { ChatMessage, ChatMessageRequest } from '../../../models/chat-message.model';
import { ConnectionStatus, WebSocketConnectionService } from '../../../services/ws/web-socket-connection-service';
import { Subscription } from 'rxjs';
import { ChatService } from '../../../services/chat/chat-service';
import { RoomHistoryService } from '../../../services/room/room-history-service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-chat-room-component',
  imports: [CommonModule,FormsModule],
  templateUrl: './chat-room-component.html',
  styleUrl: './chat-room-component.scss',
})
export class ChatRoomComponent implements OnInit, OnChanges, OnDestroy {
  @Input({ required: true }) roomId!: string;
  @Input({ required: true }) username!: string;

  messages: ChatMessage[] = [];
  newMessageContent = '';
  connectionStatus: ConnectionStatus = 'disconnected';

  private subscriptions: Subscription[] = [];

  constructor(
    private chatService: ChatService,
    private wsConnection: WebSocketConnectionService,
    private historyService: RoomHistoryService
  ) {}

  ngOnInit(): void {
    this.subscriptions.push(
      this.wsConnection.status$.subscribe(status => this.connectionStatus = status),
      this.chatService.messages$.subscribe(message => this.messages.push(message))
    );
    this.loadRoom();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['roomId'] && !changes['roomId'].firstChange) {
      this.messages = [];
      this.loadRoom();
    }
  }

  private loadRoom(): void {
    this.historyService.getHistory(this.roomId).subscribe(
      history => this.messages = history
    );
    this.chatService.joinRoom(this.roomId, this.username);
  }

  sendMessage(): void {
    if (!this.newMessageContent.trim()) {
      return;
    }
    this.chatService.sendMessage(this.roomId, this.username, this.newMessageContent);
    this.newMessageContent = '';
  }

  ngOnDestroy(): void {
    this.chatService.leaveRoom();
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }
}