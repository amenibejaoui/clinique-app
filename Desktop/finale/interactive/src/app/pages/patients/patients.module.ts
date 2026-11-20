import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgbModalModule } from '@ng-bootstrap/ng-bootstrap';

import { PatientsRoutingModule } from './patients-routing.module';
import { PatientsComponent } from './patients.component';

@NgModule({
  declarations: [PatientsComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    NgbModalModule,
    PatientsRoutingModule
  ]
})
export class PatientsModule { }
