import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { Cliente } from '../models/cliente.model';
import { AuthService } from '../services/auth.service';

export const meuClienteResolver: ResolveFn<Cliente> = () => inject(AuthService).meuCadastro();
