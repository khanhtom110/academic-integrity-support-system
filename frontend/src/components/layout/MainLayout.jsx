import Header from "./Header";
import "./MainLayout.css";

function MainLayout({ children }) {
  return (
    <div className="main-layout">
      <Header />
      <main className="main-content">
        <div className="main-content-wrapper">
          {children}
        </div>
      </main>
    </div>
  );
}

export default MainLayout;
