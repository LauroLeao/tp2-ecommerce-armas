import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, MatIconModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cd = inject(ChangeDetectorRef);
  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', Validators.required],
  });
  carregando = false;
  erro = '';
  mostrarSenha = false;

  entrar(): void {
    if (this.carregando) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.carregando = true;
    this.erro = '';
    const { email, senha } = this.form.getRawValue();
    this.auth.entrar(email, senha).subscribe({
      next: (usuario) => {
        this.carregando = false;
        if (usuario) this.router.navigate(['/painel']);
        else this.erro = 'Não foi possível abrir a sessão. Tente novamente.';
        this.cd.markForCheck();
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando = false;
        this.erro = erro.status === 401 ? 'E-mail ou senha incorretos.' : 'Não foi possível acessar o servidor. Tente novamente.';
        this.cd.markForCheck();
      },
    });
  }
}
