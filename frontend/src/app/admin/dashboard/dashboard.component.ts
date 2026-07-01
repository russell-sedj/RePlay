import { Component, OnInit } from '@angular/core';
import { AdminService, StatsDTO } from '../admin.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  stats!: StatsDTO;
  loading = true;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getStats().subscribe(s => {
      this.stats = s;
      this.loading = false;
    });
  }
}
