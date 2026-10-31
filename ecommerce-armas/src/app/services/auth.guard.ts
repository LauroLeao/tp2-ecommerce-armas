import { PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService, Sessao } from './auth.service';

const permitir = (usuario: Sessao | null, perfil: Sessao['perfil'] | undefined, router: Router) => {
  if (!usuario) return router.createUrlTree(['/login']);
  if (perfil && usuario.perfil !== perfil) return router.createUrlTree(['/painel']);
  return true;
};

export const exigirPerfil = (perfil?: Sessao['perfil']): CanActivateFn => () => {
  if (!isPlatformBrowser(inject(PLATFORM_ID))) return true;
  const auth = inject(AuthService);
  const router = inject(Router);
  const usuario = auth.usuario();
  if (usuario) return permitir(usuario, perfil, router);
  return auth.carregar().pipe(map((sessao) => permitir(sessao, perfil, router)));
};

export const redirecionarPainel: CanActivateFn = () => {
  if (!isPlatformBrowser(inject(PLATFORM_ID))) return true;
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.carregar().pipe(map((usuario) => usuario ? router.createUrlTree(['/painel']) : true));
};
