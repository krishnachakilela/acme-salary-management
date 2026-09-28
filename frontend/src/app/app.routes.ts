import {Routes} from '@angular/router';
import {authGuard} from '../core/auth.guard';
import { LoginComponent } from '../features/auth/login/login.component';
import {EmployeeListComponent} from '../features/employees/employee-list.component';
import { EmployeeDetailComponent } from '../features/employees/employee-detail.component';
import {EmployeeFormComponent} from '../features/employees/employee-form.component';
import { AnalyticsComponent } from '../features/analytics/analytics.component';

export const routes: Routes = [
    {path: 'login', component: LoginComponent},
    {
        path: '', canActivate: [authGuard], children: [
            {path: '', pathMatch: 'full', redirectTo: 'employees'},
            {path: 'employees', component: EmployeeListComponent},
            {path: 'employees/new', component: EmployeeFormComponent},
            {path: 'employees/:id', component: EmployeeDetailComponent},
            {path: 'analytics', component: AnalyticsComponent}
        ]
    },
    {path: '**', redirectTo: 'employees'}
];
 