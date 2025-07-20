import React, { useState, useEffect } from 'react';
import axios from 'axios';

function AddDistributorItem() {
  const [distName, setDistName] = useState('');
  const [itemName, setItemName] = useState('');
  const [cost, setCost] = useState(0);
  const [message, setMessage] = useState('');
  const [distributors, setDistributors] = useState([]);
  const [items, setItems] = useState([]);

  useEffect(() => {
    // Fetch distributors and items when component mounts
    fetchDistributors();
    fetchItems();
  }, []);

  const fetchDistributors = async () => {
    try {
      const response = await axios.get('http://localhost:4567/distributors');
      setDistributors(response.data);
    } catch (error) {
      console.error('Error fetching distributors:', error);
    }
  };

  const fetchItems = async () => {
    try {
      const response = await axios.get('http://localhost:4567/items');
      setItems(response.data);
    } catch (error) {
      console.error('Error fetching items:', error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:4567/add_distributor_item', {
        distName,
        itemName,
        cost: parseFloat(cost)
      }, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      setMessage('Item added to distributor catalog successfully!');
      setDistName('');
      setItemName('');
      setCost(0);
    } catch (error) {
      console.error('Error adding item to distributor:', error);
      setMessage(`Error: ${error.response?.data || error.message}`);
    }
  };

  return (
    <div className="form-container">
      <h3>Add Item to Distributor Catalog</h3>
      {message && <div className="message">{message}</div>}
      <form onSubmit={handleSubmit}>
        <div>
          <label>Distributor:</label>
          <select 
            value={distName} 
            onChange={(e) => setDistName(e.target.value)}
            required
          >
            <option value="">Select a distributor</option>
            {distributors.map(dist => (
              <option key={dist.id} value={dist.name}>{dist.name}</option>
            ))}
          </select>
        </div>
        <div>
          <label>Item:</label>
          <select 
            value={itemName} 
            onChange={(e) => setItemName(e.target.value)}
            required
          >
            <option value="">Select an item</option>
            {items.map(item => (
              <option key={item.id} value={item.name}>{item.name}</option>
            ))}
          </select>
        </div>
        <div>
          <label>Cost:</label>
          <input 
            type="number" 
            step="0.01"
            value={cost} 
            onChange={(e) => setCost(e.target.value ? parseFloat(e.target.value) : 0)} 
            required 
          />
        </div>
        <button type="submit">Add to Catalog</button>
      </form>
    </div>
  );
}

export default AddDistributorItem;