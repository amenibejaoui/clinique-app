import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CalendarModule, DateAdapter } from 'angular-calendar';
import { adapterFactory } from 'angular-calendar/date-adapters/date-fns';
import { ToastrModule } from 'ngx-toastr';

import { RendezVousComponent } from './rendez-vous.component';

describe('RendezVousComponent', () => {
  let component: RendezVousComponent;
  let fixture: ComponentFixture<RendezVousComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RendezVousComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(RendezVousComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
