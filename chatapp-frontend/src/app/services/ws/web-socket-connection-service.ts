import { Injectable } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { BehaviorSubject, Observable } from 'rxjs';
import SockJS from 'sockjs-client';

export type ConnectionStatus = 'connected' | 'connecting' | 'disconnected';
@Injectable({
  providedIn: 'root',
})
export class WebSocketConnectionService {

  private client:Client;
  private statusSubject = new BehaviorSubject<ConnectionStatus>('disconnected');

  status$: Observable<ConnectionStatus> = this.statusSubject.asObservable();

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      onConnect: () => this.statusSubject.next('connected'),
      onDisconnect: () => this.statusSubject.next('disconnected'),
      onWebSocketClose: () => this.statusSubject.next('connecting')
    });
  }

  connect(): void {
    this.statusSubject.next('connecting');
    this.client.activate();
  }

  disconnect(): void {
    this.client.deactivate();
  }

  subscribe(destination: string, onMessage: (message: IMessage) => void): StompSubscription {
    return this.client.subscribe(destination, onMessage);
  }

  publish(destination: string, body: unknown): void {
    this.client.publish({ destination, body: JSON.stringify(body) });
  }
  
}
