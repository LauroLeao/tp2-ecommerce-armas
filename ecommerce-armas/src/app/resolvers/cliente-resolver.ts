import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { Cliente } from '../models/cliente.model';
import { ClienteService } from '../services/cliente.service';

export const clienteResolver: ResolveFn<Cliente> = (route) => {
  const id = Number(route.paramMap.get('id'));
  return inject(ClienteService).buscarPorId(id);
};
