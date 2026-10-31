import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { AuthService } from './services/auth.service';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])]
    })
      .compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the admin navigation only for administrators', async () => {
    const fixture = TestBed.createComponent(App);
    TestBed.inject(AuthService).usuario.set({ email: 'admin@teste.local', nome: 'Administrador', perfil: 'ADMIN', clienteId: null });
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Painel inicial');
    expect(compiled.textContent).toContain('Produtos');
    expect(compiled.textContent).toContain('Clientes');
  });

  it('should hide the admin menu for customers', () => {
    const fixture = TestBed.createComponent(App);
    TestBed.inject(AuthService).usuario.set({ email: 'cliente@teste.local', nome: 'Cliente', perfil: 'CLIENTE', clienteId: 1 });
    fixture.detectChanges();
    const menu = fixture.nativeElement.querySelector('.sidebar') as HTMLElement;
    expect(menu.textContent).toContain('Painel inicial');
    expect(menu.textContent).not.toContain('Fornecedores');
  });

  it('alternates the theme and remembers the choice', () => {
    localStorage.removeItem('arsenal-tema');
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('.theme-toggle') as HTMLButtonElement;
    expect(button.getAttribute('aria-label')).toBe('Ativar modo claro');
    button.click();
    fixture.detectChanges();
    expect(document.documentElement.dataset['theme']).toBe('claro');
    expect(localStorage.getItem('arsenal-tema')).toBe('claro');
    expect(button.getAttribute('aria-label')).toBe('Ativar modo escuro');
    button.click();
    expect(document.documentElement.dataset['theme']).toBe('escuro');
    localStorage.removeItem('arsenal-tema');
  });
});
