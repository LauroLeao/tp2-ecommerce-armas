import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';

import { Produto } from '../models/produto.model';
import { ProdutoService } from '../services/produto.service';

export const produtoResolver: ResolveFn<Produto> = (route) => {

  const id = Number(route.paramMap.get('id'));

  return inject(ProdutoService).buscarPorId(id);
};