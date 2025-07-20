import React, { useState } from 'react';
import './App.css';
import InventoryList from './components/InventoryList';
import AddInventoryItem from './components/AddInventoryItem';
import DistributorList from './components/DistributorList';
import AddDistributor from './components/AddDistributor';
import CheapestRestock from './components/CheapestRestock';

function App() {
  const [activeTab, setActiveTab] = useState('inventory');

  return (
    <div className="App">
      <header className="App-header">
        <h1>TopBloc Candy Inventory Management</h1>
        <div className="tabs">
          <button 
            className={activeTab === 'inventory' ? 'active' : ''} 
            onClick={() => setActiveTab('inventory')}>
            Inventory
          </button>
          <button 
            className={activeTab === 'distributors' ? 'active' : ''} 
            onClick={() => setActiveTab('distributors')}>
            Distributors
          </button>
          <button 
            className={activeTab === 'restock' ? 'active' : ''} 
            onClick={() => setActiveTab('restock')}>
            Restock
          </button>
        </div>
      </header>
      <main>
        {activeTab === 'inventory' && (
          <div>
            <h2>Inventory</h2>
            <AddInventoryItem />
            <InventoryList />
          </div>
        )}
        {activeTab === 'distributors' && (
          <div>
            <h2>Distributors</h2>
            <AddDistributor />
            <DistributorList />
          </div>
        )}
        {activeTab === 'restock' && (
          <div>
            <h2>Find Cheapest Restock Option</h2>
            <CheapestRestock />
          </div>
        )}
      </main>
    </div>
  );
}

export default App;