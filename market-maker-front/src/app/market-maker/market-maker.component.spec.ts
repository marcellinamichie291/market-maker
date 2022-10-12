import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MarketMakerComponent } from './market-maker.component';

describe('MarketMakerComponent', () => {
  let component: MarketMakerComponent;
  let fixture: ComponentFixture<MarketMakerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MarketMakerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MarketMakerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
