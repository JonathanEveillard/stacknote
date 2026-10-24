import { Component, signal } from '@angular/core';

@Component({
  imports: [],
  selector: 'app-todo-list',
  styleUrl: './todo-list.css',
  templateUrl: './todo-list.html',
})
export class TodoList {
  todos = signal<string[]>([]);
  newTodo=signal('');

  addTodo() {
    if (!this.newTodo().trim) return;
    this.todos.update(list => [...list, this.newTodo()]);
    this.newTodo.set('');
  }
}
