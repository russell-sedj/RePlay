import { Component, OnInit } from '@angular/core';
import { AdminService, UserAdminDTO } from '../admin.service';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html'
})
export class UserListComponent implements OnInit {
  users: UserAdminDTO[] = [];
  loading = true;
  currentPage = 0;
  totalPages = 0;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.adminService.getUsers(this.currentPage).subscribe(res => {
      this.users = res.content;
      this.totalPages = res.totalPages;
      this.loading = false;
    });
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.loadUsers();
  }
}
