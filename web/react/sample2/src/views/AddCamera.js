import React from 'react';
import { Link } from 'react-router-dom';
import { context } from '../context';
import '../App.css';

class AddCamera extends React.Component {
    static contextType = context;

    constructor() {
        super();
        this.state = {
            shopName: "",
        }

        this.onShopNameChange = this.onShopNameChange.bind(this);
    }

    onShopNameChange(event) {
        this.setState({
            shopName: event.target.value,
        });
    }
    
    render() {
        const { shop_list } = this.context;

        console.log(this.props);
        return (
            <div className="App">
                <header className="App-header">
                    <div className="Toolbar">
                        <Link className="HomeButton" to={`/shop/${this.props.match.params.shop}`}>&lt;</Link>
                        新增攝影機
                        <input id="shopName" className="Input" onChange={this.onShopNameChange}></input>
                        <button className="AddShopButton" onClick={() => this.props.onAddCamera(this.props.match.params.shop, this.state.shopName)}>新增</button>
                    </div>
                </header>
            </div>
        )    
    }
}

export default AddCamera;