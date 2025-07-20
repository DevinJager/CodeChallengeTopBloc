import React, { useState } from 'react';
import axios from 'axios';

function AddInventoryItem() {
  const [name, setName] = useState('');
  const [stock, setStock] = useState(0);
  const [capacity, setCapacity] = useState(0);
  const [message, setMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // Convert stock and capacity to numbers to ensure they're sent as numbers
      const stockNum = parseInt(stock);
      const capacityNum = parseInt(capacity);
      
      console.log('Sending data:', { name, stock: stockNum, capacity: capacityNum });
      
      // Use the full URL to ensure it reaches the correct endpoint
      const response = await axios.post('http://localhost:4567/add_inventory', {
        name,
        stock: stockNum,
        capacity: capacityNum
      }, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      console.log('Response:', response.data);
      setMessage('Item added successfully!');
      setName('');
      setStock(0);
      setCapacity(0);
    } catch (error) {
      console.error('Error adding item:', error);
      setMessage(`Error: ${error.response?.data || error.message}`);
    }
  };

  return (
    <div className="form-container">
      <h3>Add New Item</h3>
      {message && <div className="message">{message}</div>}
      <form onSubmit={handleSubmit}>
        <div>
          <label>Name:</label>
          <input 
            type="text" 
            value={name} 
            onChange={(e) => setName(e.target.value)} 
            required 
          />
        </div>
        <div>
          <label>Stock:</label>
          <input 
            type="number" 
            value={stock} 
            onChange={(e) => setStock(e.target.value ? parseInt(e.target.value) : 0)} 
            required 
          />
        </div>
        <div>
          <label>Capacity:</label>
          <input 
            type="number" 
            value={capacity} 
            onChange={(e) => setCapacity(e.target.value ? parseInt(e.target.value) : 0)} 
            required 
          />
        </div>
        <button type="submit">Add Item</button>
      </form>
    </div>
  );
}

export default AddInventoryItem;