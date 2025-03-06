import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import classNames from 'classnames';

const Sidebar = ({ navigation }) => {
  const location = useLocation();
  const { user } = useAuth();

  return (
    <div className="flex grow flex-col gap-y-5 overflow-y-auto bg-primary-600 px-6 pb-4">
      <div className="flex h-16 shrink-0 items-center">
        <Link to="/" className="flex items-center">
          <img
            className="h-8 w-auto"
            src="/logo.svg"
            alt="EMS"
          />
          <span className="ml-2 text-xl font-semibold text-white">EMS</span>
        </Link>
      </div>
      <nav className="flex flex-1 flex-col">
        <ul role="list" className="flex flex-1 flex-col gap-y-7">
          <li>
            <ul role="list" className="-mx-2 space-y-1">
              {navigation.map((item) => (
                <li key={item.name}>
                  <Link
                    to={item.href}
                    className={classNames(
                      location.pathname === item.href
                        ? 'bg-primary-700 text-white'
                        : 'text-primary-100 hover:text-white hover:bg-primary-700',
                      'group flex gap-x-3 rounded-md p-2 text-sm leading-6 font-semibold'
                    )}
                  >
                    <item.icon
                      className={classNames(
                        location.pathname === item.href
                          ? 'text-white'
                          : 'text-primary-200 group-hover:text-white',
                        'h-6 w-6 shrink-0'
                      )}
                      aria-hidden="true"
                    />
                    {item.name}
                  </Link>
                </li>
              ))}
            </ul>
          </li>
          <li className="mt-auto">
            <div className="flex items-center gap-x-4 px-6 py-3 text-sm font-semibold leading-6 text-white">
              <div className="h-8 w-8 rounded-full bg-primary-700 flex items-center justify-center">
                <span className="text-sm font-medium leading-none text-white">
                  {user?.firstName?.[0]}
                  {user?.lastName?.[0]}
                </span>
              </div>
              <div className="flex flex-col">
                <span className="text-white">
                  {user?.firstName} {user?.lastName}
                </span>
                <span className="text-primary-200 text-xs">
                  {user?.role === 'ROLE_ADMIN' ? 'Administrator' : 'Employee'}
                </span>
              </div>
            </div>
          </li>
        </ul>
      </nav>
    </div>
  );
};

export default Sidebar;
