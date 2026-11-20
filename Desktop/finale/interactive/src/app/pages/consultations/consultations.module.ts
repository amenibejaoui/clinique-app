import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgbModalModule } from '@ng-bootstrap/ng-bootstrap';

import { ConsultationsRoutingModule } from './consultations-routing.module';
import { ConsultationsComponent } from './consultations.component';

@NgModule({
  declarations: [ConsultationsComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    NgbModalModule,
    ConsultationsRoutingModule
  ]
})
export class ConsultationsModule { }
