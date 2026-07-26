import { TestBed } from '@angular/core/testing';

import { WebSocketConnectionService } from './web-socket-connection-service';

describe('WebSocketConnectionService', () => {
  let service: WebSocketConnectionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WebSocketConnectionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
