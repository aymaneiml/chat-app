import { TestBed } from '@angular/core/testing';

import { RoomHistoryService } from './room-history-service';

describe('RoomHistoryService', () => {
  let service: RoomHistoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RoomHistoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
