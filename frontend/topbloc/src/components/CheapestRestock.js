import React, { useState } from 'react';
import axios from 'axios';

function CheapestRestock() {
  const [itemName, setItemName] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // Use the full URL to ensure it reaches the correct endpoint
      const response = await axios.get(`http://localhost:4567/cheapest_restock?itemName=${encodeURIComponent(itemName)}&quantity=${quantity}`);
      setResult(response.data);
      setError('');
    } catch (error) {
      setError(`Error: ${error.response?.data || error.message}`);
      setResult(null);
    }
  };

  return (
    <div className="form-container">
      <form onSubmit={handleSubmit}>
        <div>
          <label>Item Name:</label>
          <input 
            type="text" 
            value={itemName} 
            onChange={(e) => setItemName(e.target.value)} 
            required 
          />
        </div>
        <div>
          <label>Quantity:</label>
          <input 
            type="number" 
            value={quantity} 
            onChange={(e) => setQuantity(e.target.value ? parseInt(e.target.value) : 1)} 
            min="1"
            required 
          />
        </div>
        <button type="submit">Find Cheapest Option</button>
      </form>

      {error && <div className="error">{error}</div>}
      
      {result && (
        <div className="result">
          <h3>Cheapest Option</h3>
          <p><strong>Item:</strong> {result.item}</p>
          <p><strong>Quantity:</strong> {result.quantity}</p>
          <p><strong>Distributor:</strong> {result.distributor}</p>
          <p><strong>Unit Cost:</strong> ${result.unit_cost?.toFixed(2) || "N/A"}</p>
          <p><strong>Total Cost:</strong> ${result.total_cost?.toFixed(2) || "N/A"}</p>
        </div>
      )}
    </div>
  );
}

export default CheapestRestock;