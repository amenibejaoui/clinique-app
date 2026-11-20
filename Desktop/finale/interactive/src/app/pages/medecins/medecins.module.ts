import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgbModalModule } from '@ng-bootstrap/ng-bootstrap';
import { MedecinsComponent } from './medecins.component';
import { MedecinsRoutingModule } from './medecins-routing.module';

@NgModule({
  declarations: [MedecinsComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    NgbModalModule,
    MedecinsRoutingModule
  ]
})
export class MedecinsModule { }
