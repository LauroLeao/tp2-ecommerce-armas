import { Routes } from '@angular/router';
import { ProdutoList } from './components/produtos/produto-list/produto-list';
import { ProdutoForm } from './components/produtos/produto-form/produto-form';
import { produtoResolver } from './resolvers/produto-resolver';
import { ClienteList } from './components/clientes/cliente-list/cliente-list';
import { ClienteForm } from './components/clientes/cliente-form/cliente-form';
import { clienteResolver } from './resolvers/cliente-resolver';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'produtos'
  },
  {
    path: 'produtos',
    component: ProdutoList,
    title: 'Lista de Produtos'
  },
  {
    path: 'produtos/new',
    component: ProdutoForm,
    title: 'Novo Produto'
  },
  {
    path: 'produtos/edit/:id',
    component: ProdutoForm,
    title: 'Editar Produto',
    resolve: {
      produto: produtoResolver
    }
  },
  {
    path: 'clientes',
    component: ClienteList,
    title: 'Lista de Clientes'
  },
  {
    path: 'clientes/new',
    component: ClienteForm,
    title: 'Novo Cliente'
  },
  {
    path: 'clientes/edit/:id',
    component: ClienteForm,
    title: 'Editar Cliente',
    resolve: {
      cliente: clienteResolver
    }
  },
  {
    path: '**',
    redirectTo: 'produtos'
  }
];
