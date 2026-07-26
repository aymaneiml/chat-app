import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ChatMessage } from '../../models/chat-message.model';

@Injectable({
  providedIn: 'root',
})
export class RoomHistoryService {
  
  constructor(private http:HttpClient){}

  getHistory(roomId: string): Observable<ChatMessage[]>{
    return this.http.get<ChatMessage[]>(`/api/room/${roomId}/messages`);
  }
}
