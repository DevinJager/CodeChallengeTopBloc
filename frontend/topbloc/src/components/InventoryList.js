import React, { useState, useEffect } from 'react';
import axios from 'axios';

function InventoryList() {
  const [inventory, setInventory] = useState([]);
  const [filter, setFilter] = useState('all');

  useEffect(() => {
    fetchInventory();
  }, [filter]);

  const fetchInventory = async () => {
    try {
      let endpoint = 'http://localhost:4567/inventory';
      if (filter === 'nostock') endpoint = 'http://localhost:4567/nostock';
      if (filter === 'overstock') endpoint = 'http://localhost:4567/overstock';
      if (filter === 'lowstock') endpoint = 'http://localhost:4567/lowstock';
      
      const response = await axios.get(endpoint);
      setInventory(response.data);
    } catch (error) {
      console.error('Error fetching inventory:', error);
    }
  };

  const handleDelete = async (name) => {
    try {
      await axios.delete(`http://localhost:4567/delete_inventory?name=${encodeURIComponent(name)}`);
      fetchInventory();
    } catch (error) {
      console.error('Error deleting item:', error);
    }
  };

  return (
    <div>
      <div className="filters">
        <button onClick={() => setFilter('all')} className={filter === 'all' ? 'active' : ''}>All</button>
        <button onClick={() => setFilter('nostock')} className={filter === 'nostock' ? 'active' : ''}>No Stock</button>
        <button onClick={() => setFilter('overstock')} className={filter === 'overstock' ? 'active' : ''}>Overstock</button>
        <button onClick={() => setFilter('lowstock')} className={filter === 'lowstock' ? 'active' : ''}>Low Stock</button>
      </div>
      
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>ID</th>
            <th>Stock</th>
            <th>Capacity</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {inventory.map(item => (
            <tr key={item.id}>
              <td>{item.name}</td>
              <td>{item.id}</td>
              <td>{item.stock}</td>
              <td>{item.capacity}</td>
              <td>
                <button onClick={() => handleDelete(item.name)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default InventoryList;