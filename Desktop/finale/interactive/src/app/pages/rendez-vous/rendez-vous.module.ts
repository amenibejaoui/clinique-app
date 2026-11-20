import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RendezVousComponent } from './rendez-vous.component';
import { RendezVousRoutingModule } from './rendez-vous-routing.module';

@NgModule({
  declarations: [RendezVousComponent],
  imports: [CommonModule, FormsModule, RendezVousRoutingModule]
})
export class RendezVousModule {}
