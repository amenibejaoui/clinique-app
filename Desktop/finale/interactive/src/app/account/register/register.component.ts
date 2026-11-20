import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { first } from 'rxjs/operators';

import { AuthenticationService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})

/**
 * Register Component
 */
export class RegisterComponent implements OnInit {

  signupForm!: UntypedFormGroup;
  submitted = false;
  successmsg = false;
  error = '';
  year: number = new Date().getFullYear();

  constructor(
    private formBuilder: UntypedFormBuilder,
    private router: Router,
    private authenticationService: AuthenticationService
  ) { }

  ngOnInit(): void {
    this.signupForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      name: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      tel: ['']
    });
  }

  get f() { return this.signupForm.controls; }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';

    if (this.signupForm.invalid) {
      return;
    }

    const rawName = (this.f['name'].value || '').trim();
    const parts = rawName.split(/\s+/);
    const nom = parts[0] || rawName;
    const prenom = parts.slice(1).join(' ') || parts[0] || nom;

    const payload: any = {
      email: (this.f['email'].value || '').trim(),
      password: this.f['password'].value,
      nom,
      prenom
    };

    const rawTel = (this.f['tel']?.value || '').trim();
    if (rawTel) {
      payload.tel = rawTel;
    }

    this.authenticationService.register(payload)
      .pipe(first())
      .subscribe({
        next: () => {
          this.successmsg = true;
          this.router.navigate(['/auth/login']);
        },
        error: (err: any) => {
          this.error = err?.error?.message || err?.message || 'Erreur lors de l\'inscription';
        }
      });
  }
}
