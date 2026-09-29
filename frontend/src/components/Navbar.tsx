import { useState } from "react";
import { Link, NavLink, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function SunMark() {
  return (
    <svg className="brand-mark" viewBox="0 0 48 24" aria-hidden="true">
      <path d="M3 21C12 3 35 3 45 21" />
      <circle cx="24" cy="7" r="3" />
    </svg>
  );
}

export default function Navbar() {
  const { token, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const location = useLocation();
  const links = token
    ? [
        ["/dashboard", "Dashboard"],
        ["/assessment", "Assessment"],
        ["/panels", "Panels"],
        ["/assistant", "Assistant"],
      ]
    : [
        ["/login", "Log in"],
        ["/register", "Create account"],
      ];

  function closeMenu() {
    setOpen(false);
  }

  return (
    <>
      <a className="skip-link" href="#main-content">
        Skip to content
      </a>
      <header
        className={`site-header ${location.pathname === "/" ? "site-header-home" : ""}`}
      >
        <Link className="brand" to="/" onClick={closeMenu}>
          <SunMark />
          <span>SolarMind</span>
        </Link>
        <button
          className="menu-toggle"
          aria-expanded={open}
          aria-controls="primary-nav"
          onClick={() => setOpen(!open)}
        >
          <span>{open ? "Close" : "Menu"}</span>
        </button>
        <nav
          id="primary-nav"
          className={`primary-nav ${open ? "primary-nav-open" : ""}`}
          aria-label="Primary navigation"
        >
          {links.map(([href, label]) => (
            <NavLink
              key={href}
              to={href}
              onClick={closeMenu}
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              {label}
            </NavLink>
          ))}
          {token && (
            <button
              className="nav-logout"
              onClick={() => {
                logout();
                closeMenu();
              }}
            >
              Log out
            </button>
          )}
        </nav>
      </header>
    </>
  );
}
