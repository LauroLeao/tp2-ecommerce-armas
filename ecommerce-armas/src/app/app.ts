import { Component, PLATFORM_ID, inject, signal } from '@angular/core';
import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule, MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { ARSENAL_ICONS } from './icons';
import { AuthService } from './services/auth.service';

@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatButtonModule, MatIconModule],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly document = inject(DOCUMENT);
  private readonly browser = isPlatformBrowser(inject(PLATFORM_ID));
  constructor(icons: MatIconRegistry, sanitizer: DomSanitizer) {
    icons.addSvgIconSetLiteral(sanitizer.bypassSecurityTrustHtml(ARSENAL_ICONS));
    if (this.browser) {
      try {
        const guardado = this.document.defaultView?.localStorage.getItem('arsenal-tema');
        if (guardado === 'claro') this.tema.set('claro');
      } catch {
        // Armazenamento do navegador pode estar indisponível.
      }
      this.document.documentElement.dataset['theme'] = this.tema();
    }
  }
  readonly tema = signal<'escuro' | 'claro'>('escuro');
  readonly menuAberto = signal(false);
  alternarTema(): void {
    const proximo = this.tema() === 'escuro' ? 'claro' : 'escuro';
    this.tema.set(proximo);
    if (this.browser) {
      this.document.documentElement.dataset['theme'] = proximo;
    }
    try {
      this.document.defaultView?.localStorage.setItem('arsenal-tema', proximo);
    } catch {
      // A troca visual continua funcionando sem armazenamento.
    }
  }
  fecharMenu(): void {
    this.menuAberto.set(false);
  }
  sair(): void {
    this.auth.sair().subscribe({
      next: () => {
        this.fecharMenu();
        this.router.navigate(['/login']);
      },
      error: () => this.document.defaultView?.alert('Não foi possível sair agora. Tente novamente.'),
    });
  }
}
