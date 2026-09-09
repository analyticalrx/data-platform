-- Manual migration: simplified one-tenant-per-user RBAC model.
-- This migration intentionally replaces only empty legacy control-plane identity/RBAC tables.
-- It preserves control_plane.tenant_registry and every tenant-owned schema/table.

begin;

do $$
begin
  if (select count(*) from control_plane.app_user) <> 0
     or (select count(*) from control_plane.user_tenant_access) <> 0
     or (select count(*) from control_plane.user_role_assignment) <> 0
     or (select count(*) from control_plane.user_role_pharmacy_scope) <> 0
     or (select count(*) from control_plane.permission_audit_event) <> 0
     or (select count(*) from control_plane.app_access_audit_event) <> 0 then
    raise exception 'Legacy control-plane identity/RBAC tables contain data; migration aborted.';
  end if;
end
$$;

drop table control_plane.app_access_audit_event;
drop table control_plane.permission_audit_event;
drop table control_plane.user_role_pharmacy_scope;
drop table control_plane.user_role_assignment;
drop table control_plane.user_tenant_access;
drop table control_plane.role_definition;
drop table control_plane.app_user;

create table control_plane.app_user (
  id bigint generated always as identity primary key,
  tenant_id uuid not null references control_plane.tenant_registry(id),
  display_name text not null,
  email text not null,
  password_hash text not null,
  password_algorithm text not null default 'bcrypt',
  phone_number text,
  job_title text,
  status text not null default 'active' check (
    status in ('invited', 'active', 'inactive', 'password_reset_required')
  ),
  permissions_version integer not null default 1 check (permissions_version > 0),
  last_login_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (id, tenant_id)
);

create unique index uq_app_user_tenant_email_lower
  on control_plane.app_user (tenant_id, lower(email));

create table control_plane.user_pharmacy_access (
  id bigint generated always as identity primary key,
  user_id bigint not null,
  tenant_id uuid not null,
  pharmacy_key text not null,
  status text not null default 'active' check (status in ('active', 'inactive')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  foreign key (user_id, tenant_id)
    references control_plane.app_user(id, tenant_id) on delete cascade,
  unique (user_id, tenant_id, pharmacy_key)
);

create table control_plane.role_definition (
  id smallint generated always as identity primary key,
  role_code text not null unique,
  display_name text not null,
  description text not null,
  status text not null default 'active' check (status in ('active', 'inactive')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table control_plane.permission_definition (
  id smallint generated always as identity primary key,
  permission_code text not null unique,
  display_name text not null,
  description text not null,
  status text not null default 'active' check (status in ('active', 'inactive')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table control_plane.role_permission (
  role_id smallint not null references control_plane.role_definition(id) on delete cascade,
  permission_id smallint not null references control_plane.permission_definition(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (role_id, permission_id)
);

create table control_plane.user_role (
  user_id bigint not null references control_plane.app_user(id) on delete cascade,
  role_id smallint not null references control_plane.role_definition(id),
  status text not null default 'active' check (status in ('active', 'inactive')),
  assigned_by_user_id bigint references control_plane.app_user(id),
  assigned_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  primary key (user_id, role_id)
);

create table control_plane.permission_audit_event (
  id bigint generated always as identity primary key,
  actor_user_id bigint references control_plane.app_user(id),
  target_user_id bigint references control_plane.app_user(id),
  tenant_id uuid references control_plane.tenant_registry(id),
  action text not null,
  resource_type text,
  resource_id text,
  old_value jsonb,
  new_value jsonb,
  reason text,
  request_id text,
  created_at timestamptz not null default now()
);

create table control_plane.app_access_audit_event (
  id bigint generated always as identity primary key,
  user_id bigint references control_plane.app_user(id),
  tenant_id uuid references control_plane.tenant_registry(id),
  event_type text not null check (
    event_type in ('login_success', 'login_failed', 'logout', 'token_issued', 'token_rejected', 'password_changed', 'sensitive_read_denied', 'admin_action')
  ),
  resource_type text,
  resource_id text,
  status text not null default 'success' check (status in ('success', 'failure', 'denied')),
  request_id text,
  ip_address_hash text,
  user_agent_hash text,
  details jsonb,
  created_at timestamptz not null default now()
);

create index idx_app_user_tenant_status on control_plane.app_user (tenant_id, status);
create index idx_user_pharmacy_access_user on control_plane.user_pharmacy_access (user_id, status);
create index idx_user_role_user on control_plane.user_role (user_id, status);
create index idx_role_permission_permission on control_plane.role_permission (permission_id);
create index idx_permission_audit_tenant_created on control_plane.permission_audit_event (tenant_id, created_at);
create index idx_app_access_audit_user_created on control_plane.app_access_audit_event (user_id, created_at);

insert into control_plane.role_definition (role_code, display_name, description)
values
  ('OWNER', 'Owner', 'Tenant owner with business and dashboard visibility.'),
  ('TENANT_ADMIN', 'Tenant Admin', 'Tenant administrator who manages users and roles.'),
  ('STORE_MANAGER', 'Store Manager', 'Operations manager for assigned pharmacies.'),
  ('PHARMACIST', 'Pharmacist', 'Pharmacy clinical and operational user.'),
  ('TECHNICIAN', 'Technician', 'Pharmacy operations user.'),
  ('BOOKKEEPER', 'Bookkeeper', 'Finance and reconciliation user.'),
  ('COMPLIANCE_ADMIN', 'Compliance Admin', 'Compliance and audit user.'),
  ('BUSINESS_DEVELOPMENT', 'Business Development', 'Outreach and relationship-management user.'),
  ('INTERNAL_PLATFORM_ADMIN', 'Internal Platform Admin', 'AnalyticalRx platform administrator.')
order by 1;

insert into control_plane.permission_definition (permission_code, display_name, description)
values
  ('dashboard.owner.read', 'Read owner dashboard', 'View the owner dashboard.'),
  ('dashboard.store_manager.read', 'Read store-manager dashboard', 'View the store-manager dashboard.'),
  ('dashboard.pharmacist.read', 'Read pharmacist dashboard', 'View the pharmacist dashboard.'),
  ('dashboard.technician.read', 'Read technician dashboard', 'View the technician dashboard.'),
  ('dashboard.bookkeeper.read', 'Read bookkeeper dashboard', 'View the bookkeeper dashboard.'),
  ('dashboard.compliance.read', 'Read compliance dashboard', 'View the compliance dashboard.'),
  ('dashboard.business_development.read', 'Read business-development dashboard', 'View the business-development dashboard.'),
  ('finance.summary.read', 'Read financial summary', 'View tenant financial summaries.'),
  ('finance.reconciliation.read', 'Read financial reconciliation', 'View finance reconciliation detail.'),
  ('tenant.users.read', 'Read tenant users', 'View users in the tenant.'),
  ('tenant.users.manage', 'Manage tenant users', 'Create, update, and deactivate tenant users.'),
  ('tenant.roles.read', 'Read tenant roles', 'View tenant role assignments.'),
  ('tenant.roles.manage', 'Manage tenant roles', 'Assign and revoke tenant roles.'),
  ('platform.tenants.read', 'Read platform tenants', 'View platform tenant metadata.'),
  ('platform.tenants.manage', 'Manage platform tenants', 'Manage platform tenant metadata.')
order by 1;

insert into control_plane.role_permission (role_id, permission_id)
select r.id, p.id
from control_plane.role_definition r
join control_plane.permission_definition p on (r.role_code, p.permission_code) in (
  ('OWNER', 'dashboard.owner.read'),
  ('OWNER', 'finance.summary.read'),
  ('TENANT_ADMIN', 'dashboard.compliance.read'),
  ('TENANT_ADMIN', 'finance.summary.read'),
  ('TENANT_ADMIN', 'tenant.users.read'),
  ('TENANT_ADMIN', 'tenant.users.manage'),
  ('TENANT_ADMIN', 'tenant.roles.read'),
  ('TENANT_ADMIN', 'tenant.roles.manage'),
  ('STORE_MANAGER', 'dashboard.store_manager.read'),
  ('STORE_MANAGER', 'dashboard.pharmacist.read'),
  ('STORE_MANAGER', 'dashboard.technician.read'),
  ('STORE_MANAGER', 'dashboard.business_development.read'),
  ('PHARMACIST', 'dashboard.pharmacist.read'),
  ('PHARMACIST', 'dashboard.technician.read'),
  ('TECHNICIAN', 'dashboard.technician.read'),
  ('BOOKKEEPER', 'dashboard.bookkeeper.read'),
  ('BOOKKEEPER', 'finance.summary.read'),
  ('BOOKKEEPER', 'finance.reconciliation.read'),
  ('COMPLIANCE_ADMIN', 'dashboard.compliance.read'),
  ('BUSINESS_DEVELOPMENT', 'dashboard.business_development.read'),
  ('INTERNAL_PLATFORM_ADMIN', 'platform.tenants.read'),
  ('INTERNAL_PLATFORM_ADMIN', 'platform.tenants.manage')
);

grant usage on schema control_plane to analyticalrx_app_rw, analyticalrx_data_platform_password_rw;
grant select, insert, update, delete on all tables in schema control_plane to analyticalrx_app_rw, analyticalrx_data_platform_password_rw;
grant usage, select on all sequences in schema control_plane to analyticalrx_app_rw, analyticalrx_data_platform_password_rw;

commit;
